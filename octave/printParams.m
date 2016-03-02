
[paramValues paramIndex] = sort(theta);
col_display = {"INTERCEPT" cols{3:length(cols)}};

fprintf('feature param featureIndex\n');
for i = 1:length(theta)
    fprintf('%s %f2 %d \n', col_display{paramIndex(i)}, paramValues(i), paramIndex(i));
end
