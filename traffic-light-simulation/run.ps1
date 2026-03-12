param(
    [Parameter(Mandatory)][string]$InputFile,
    [Parameter(Mandatory)][string]$OutputFile
)

docker build -t traffic-sim .
docker run --rm -v "${PWD}:/app/data" traffic-sim "/app/data/$InputFile" "/app/data/$OutputFile"
