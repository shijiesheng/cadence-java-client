#!/bin/bash

# Check if the correct number of arguments are provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <folder>"
    echo "Example: $0 src/"
    echo "         (This example migrates all files in the src/ folder)"
    exit 1
fi

folder="$1"

# Check if the folder exists
if [ ! -d "$folder" ]; then
    echo "Error: Folder '$folder' not found."
    exit 1
fi

process_file() {
  local file="$1"

  if [[ "$file" == *"com/uber/cadence/serviceclient"* ]]; then
    return 0
  fi

  if [[ "$file" == *"com/uber/cadence/internal/compatibility"* ]]; then
    return 0
  fi

  # replace IWorkflowService to IWorkflowServiceV4
  sed -i.bak -E "s/([^A-Za-z])IWorkflowService([^A-Za-z]|$)/\1IWorkflowServiceV4\2/g" "$file"

  # replace TException
  sed -i.bak -E "s/org\.apache\.thrift\.TException/com.uber.cadence.entities.BaseError/g" "$file"
  sed -i.bak -E "s/([^A-Za-z]*)TException/\1BaseError/g" "$file"

  # replace AsyncMethodCallback
  sed -i.bak -E "s/org\.apache\.thrift\.async\.AsyncMethodCallback/com.uber.cadence.serviceclient.AsyncMethodCallback/g" "$file"

  # replace import
  sed -i.bak -E "s/import com\.uber\.cadence\.([A-Z][^.]+;)/import com.uber.cadence.entities.\1/g" "$file"
  sed -i.bak -E "s/import com\.uber\.cadence\.\*;/import com.uber.cadence.entities.*;/g" "$file"

  rm "$file.bak"
}

# Execute the function for each file
for file in $(find "$folder" -type f); do
  process_file "$file"
done

echo "Replacement complete!"

## Use find to get all files in the folder and subfolders
## Then use sed with regex for replacement on each file
## -i flag modifies the file in place
## 's/regex_to_find/string_to_replace/g' performs the substitution globally
#find "$folder" -type f -exec sh -c '
#  if [[ "$1" == *"com/uber/cadence/serviceclient"* ]]; then
#    return 0
#  fi
#
#  if [[ "$1" == *"com/uber/cadence/internal/compatibility"* ]]; then
#    return 0
#  fi
#
#  # replace IWorkflowService to IWorkflowServiceV4
#  sed -i.bak -E "s/IWorkflowService([^V]|$)/IWorkflowServiceV4\1/g" "$1"
#
#  # replace TException
#  sed -i.bak -E "s/org\.apache\.thrift\.TException/com.uber.cadence.entities.BaseError/g" "$1"
#  sed -i.bak -E "s/([^A-Za-z]*)TException/\1BaseError/g" "$1"
#
#  # replace AsyncMethodCallback
#  sed -i.bak -E "s/org\.apache\.thrift\.async\.AsyncMethodCallback/com.uber.cadence.serviceclient.AsyncMethodCallback/g" "$1"
#
#  # replace import
#  sed -i.bak -E "s/import com\.uber\.cadence\.([A-Z][^.]+;)/import com.uber.cadence.entities.\1/g" "$1"
#
#  rm "$1.bak"
#' sh {} \;


