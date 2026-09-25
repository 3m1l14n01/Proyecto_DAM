$script:SportsgdAdb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
if (-not (Test-Path -LiteralPath $script:SportsgdAdb)) {
    throw "ADB no encontrado: $script:SportsgdAdb"
}

function Get-SportsgdScreen {
    & $script:SportsgdAdb shell uiautomator dump /sdcard/sportsgd-window.xml | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo obtener el árbol de UI.' }
    $rawXml = & $script:SportsgdAdb shell cat /sdcard/sportsgd-window.xml
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo leer el árbol de UI.' }
    return [xml]($rawXml -join '')
}

function Find-SportsgdNode {
    param(
        [Parameter(Mandatory)] [xml] $Screen,
        [string] $Id,
        [string] $Text
    )
    $nodes = @($Screen.SelectNodes('//node'))
    foreach ($node in $nodes) {
        $nodeId = $node.GetAttribute('resource-id')
        $nodeText = $node.GetAttribute('text')
        if ($Id -and $nodeId -eq "com.example.sportsgd:id/$Id") { return $node }
        if ($Text -and $nodeText -eq $Text) { return $node }
    }
    throw "Control no encontrado: id='$Id' text='$Text'"
}

function Tap-SportsgdNode {
    param([Parameter(Mandatory)] $Node)
    $bounds = $Node.GetAttribute('bounds')
    $match = [regex]::Match($bounds, '^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$')
    if (-not $match.Success) { throw "Bounds inválidos: $bounds" }
    $x = [int]( ([int]$match.Groups[1].Value + [int]$match.Groups[3].Value) / 2 )
    $y = [int]( ([int]$match.Groups[2].Value + [int]$match.Groups[4].Value) / 2 )
    & $script:SportsgdAdb shell input tap $x $y | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Falló tap en $x,$y" }
    Start-Sleep -Milliseconds 350
}

function Tap-SportsgdId {
    param([Parameter(Mandatory)] [string] $Id)
    Tap-SportsgdNode (Find-SportsgdNode -Screen (Get-SportsgdScreen) -Id $Id)
}

function Tap-SportsgdText {
    param([Parameter(Mandatory)] [string] $Text)
    Tap-SportsgdNode (Find-SportsgdNode -Screen (Get-SportsgdScreen) -Text $Text)
}

function Type-SportsgdText {
    param([Parameter(Mandatory)] [string] $Text)
    & $script:SportsgdAdb shell input text $Text | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Falló entrada de texto: $Text" }
    Start-Sleep -Milliseconds 250
}

function Fill-SportsgdId {
    param([Parameter(Mandatory)] [string] $Id, [Parameter(Mandatory)] [string] $Text)
    Tap-SportsgdId $Id
    Type-SportsgdText $Text
    & $script:SportsgdAdb shell input keyevent 4 | Out-Null
    Start-Sleep -Milliseconds 200
}

function Assert-SportsgdId {
    param([Parameter(Mandatory)] [string] $Id)
    $null = Find-SportsgdNode -Screen (Get-SportsgdScreen) -Id $Id
    "OK id: $Id"
}

function Assert-SportsgdText {
    param([Parameter(Mandatory)] [string] $Text)
    $screen = Get-SportsgdScreen
    $nodes = @($screen.SelectNodes('//node'))
    if (-not ($nodes | Where-Object { $_.GetAttribute('text').Contains($Text) })) {
        throw "Texto no visible: $Text"
    }
    "OK text: $Text"
}

function Show-SportsgdScreen {
    $screen = Get-SportsgdScreen
    @($screen.SelectNodes('//node')) |
        Where-Object { $_.GetAttribute('package') -eq 'com.example.sportsgd' -and ($_.GetAttribute('text') -or $_.GetAttribute('resource-id')) } |
        ForEach-Object {
            [pscustomobject]@{
                Id = $_.GetAttribute('resource-id').Replace('com.example.sportsgd:id/', '')
                Text = $_.GetAttribute('text')
                Bounds = $_.GetAttribute('bounds')
            }
        } | Format-Table -AutoSize | Out-String -Width 220
}

function Back-Sportsgd {
    & $script:SportsgdAdb shell input keyevent 4 | Out-Null
    Start-Sleep -Milliseconds 350
}

function Swipe-SportsgdUp {
    & $script:SportsgdAdb shell input swipe 540 1900 540 700 350 | Out-Null
    Start-Sleep -Milliseconds 350
}

function Swipe-SportsgdDown {
    & $script:SportsgdAdb shell input swipe 540 700 540 1900 350 | Out-Null
    Start-Sleep -Milliseconds 350
}
