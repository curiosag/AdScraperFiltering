function printParams(theta, cols, paramThreshold, verbose)

fprintf('sizes: theta %d cols %d \n', size(theta, 1), size(cols, 1));

[paramValues paramIndex] = sort(theta);

fprintf('feature param featureIndex\n');
for i = 1:size(cols, 1)
	if abs(paramValues(i)) >= paramThreshold
		if verbose
    		fprintf('%s \t\t\t %.3f \t %d \n', cols{paramIndex(i)}, paramValues(i), paramIndex(i));
		else
			fprintf('%s\n', cols(paramIndex(i, :)));
		endif
	endif
end

end;
