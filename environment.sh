# Source this file (. ./environment.sh) to use

export GOOGLE_API_TOKEN=$(cat .lein-env | awk -F'"' '{print $2}')
