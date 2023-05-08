@echo off
set TF_LOG=INFO
set TF_LOG_PATH=.\terraform.log
set WORKERS=%1

rem FORCED FOR TEST PURPOSE
rem set WORKERS=6

rem ############################################################################################################
rem on create problems install aws cli: https://aws.amazon.com/it/cli/
rem aws iam list-roles
rem aws --region=us-east-1 iam delete-instance-profile --instance-profile-name fns-ec2-s3-instance-profile
rem ############################################################################################################

IF "%WORKERS%" == "" echo ------------------------------------------------------------------------------------------
IF "%WORKERS%" == "" echo Please specify the number of workers to create: terraform_replace.bat 7 (create 7 workers)
IF "%WORKERS%" == "" echo ------------------------------------------------------------------------------------------
IF "%WORKERS%" == "" goto end

terraform plan  -var "workers=%WORKERS%"
terraform apply -var "workers=%WORKERS%" -auto-approve

:end
pause