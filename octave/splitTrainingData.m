function [training, crossval, idxtraining, idxcrossval] = splitTrainingData(X, cvFactor)

	m = size(X, 1);
	sh = randperm(m);
	
	numCv = round(m * cvFactor);
	numTrain = m - numCv;

	%fprintf('Using: %d of %d for training %d for cross validation\n', numTrain, m, numCv);

	idxtraining = sh(1:numTrain);
	idxcrossval = sh(numTrain + 1:length(sh));

	training = X(idxtraining, :);
	crossval = X(idxcrossval, :);
end
