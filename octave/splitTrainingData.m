% split in training and cross validation sets
function [Xtrain, Xcv, ytrain, ycv, idxtrain, idxcv] = splitTrainingData(X, y, cvFactor)
	m = size(X, 1);
	sh = randperm(m);
	
	numCv = round(m * cvFactor);
	numTrain = m - numCv;

	%fprintf('Using: %d of %d for training %d for cross validation\n', numTrain, m, numCv);

	idxtrain = sh(1:numTrain);
	idxcv = sh(numTrain + 1:length(sh));

	Xtrain = X(idxtrain, :);
	Xcv = X(idxcv, :);
	ytrain = y(idxtrain, :);
	ycv =  y(idxcv, :);
end
