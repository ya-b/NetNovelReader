$ErrorActionPreference = "Stop"

$ProjectRoot = $PSScriptRoot
$BinDir = Join-Path $ProjectRoot "bin"
$EntryPoint = "./cmd/reader"

if (-not (Test-Path $BinDir)) {
    New-Item -ItemType Directory -Path $BinDir | Out-Null
}

$Builds = @(
    @{ GOOS = "windows"; GOARCH = "amd64"; Ext = ".exe" }
    @{ GOOS = "linux";   GOARCH = "amd64"; Ext = "" }
)

foreach ($b in $Builds) {
    $OutputName = "reader-$($b.GOOS)-$($b.GOARCH)$($b.Ext)"
    $OutputPath = Join-Path $BinDir $OutputName

    Write-Host "Building $OutputName ..." -ForegroundColor Cyan

    $env:GOOS = $b.GOOS
    $env:GOARCH = $b.GOARCH
    go build -ldflags="-s -w" -o $OutputPath $EntryPoint

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to build $OutputName" -ForegroundColor Red
        exit $LASTEXITCODE
    }

    Write-Host "  -> $OutputPath" -ForegroundColor Green
}

Write-Host "`nAll builds completed." -ForegroundColor Green
