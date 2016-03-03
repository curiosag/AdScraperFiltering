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
endif
	fprintf('\nwriting parameters to parameters.csv \nrelated columns to paramcols.csv\n');
	
end;
