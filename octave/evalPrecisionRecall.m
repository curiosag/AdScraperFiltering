function [precision recall] = evalPrecisionRecall(ids, predicted, actual, showFalsePositives)

	true_pos = sum(and(predicted, actual));
	true_neg = sum(and(not(predicted), not(actual)));

	false_pos_indicators = and(predicted, not(actual));
	false_pos = sum(false_pos_indicators);
	false_neg = sum(and(not(predicted), actual));

	precision = 0;
	recall = 0;

	if (true_pos + false_pos) > 0
		precision = true_pos / (true_pos + false_pos);
	endif
	if (true_pos + false_neg) > 0
		recall = true_pos / (true_pos + false_neg);
	endif
	fprintf('Precision: \t%.3f\n', precision);
	fprintf('Recall: \t%.3f\n', recall);

if showFalsePositives
	fprintf('\nFalse positives:\n');
	for i = 1:length(false_pos_indicators) * showFalsePositives
		if false_pos_indicators(i, 1) == 1
			fprintf('Id %d index %d\n', ids(i, 1), i);
		endif
	end
	fprintf('\n');
endif

end
