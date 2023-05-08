@echo off

del /q *_run_*.bat

terraform destroy -auto-approve -var "workers=0"

pause