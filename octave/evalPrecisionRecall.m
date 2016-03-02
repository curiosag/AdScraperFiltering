function [precision recall] = evalPrecisionRecall(predicted, actual)

	true_pos = sum(and(predicted, actual));
	true_neg = sum(and(not(predicted), not(actual)));
	false_pos = sum(and(predicted, not(actual)));
	false_neg = sum(and(not(predicted), actual));

	precision = true_pos / (true_pos + false_pos);
	recall = true_pos / (true_pos + false_neg);

	fprintf('Precision: %f\n', precision);
	fprintf('Recall: %f\n', recall);

end
