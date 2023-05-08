@echo off

del /q run_*.bat

terraform state list > resources.txt

for /F "tokens=*" %%A in (resources.txt) do (
		
		if "%%A" NEQ "aws_s3_bucket.fns_bucket_out" (
			echo Deleting resource: %%A

			terraform destroy -auto-approve -var "workers=0" -target %%A
		) else (
			echo Skipping resource ------------[fns_bucket_out]
		)
		rem
	)
pause