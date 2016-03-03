function printParams(theta, cols, paramThreshold, verbose, saveToFile)

if verbose
	[paramValues paramIndex] = sort(theta);
	fprintf('feature param featureIndex\n');
	for i = 1:size(cols, 1)
		if abs(paramValues(i)) >= paramThreshold
    			fprintf('%s \t\t\t %.3f \t %d \n', cols{paramIndex(i)}, paramValues(i), paramIndex(i));
		endif
	end
endif

if saveToFile
	fprintf('\nwriting parameters to parameters.txt \nrelated columns to paramcols.txt\n');
	
	save -ascii 'theta.txt' theta;

   	fid = fopen ("thetacols.txt", "w");
	for i = 1:size(cols, 1)
    	fprintf(fid, '%s\n', cols{i});
	end
    fclose (fid); 
endif

end;
