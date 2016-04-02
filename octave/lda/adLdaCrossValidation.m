% linear discriminant analysis like done here gives about 90% precision and 79% recall on training data
% seems precision cant be tuned much higer

addpath("..");
addpath("../pca");

LDA; % load functions in script file. All variables in a script file are in global scope!

[ids, status, X, y] = sanitize(load('../AdFeatures.csv'));
X = [X(:,1:7) (X(:,1) ./ X(:,2))]; % 8th fature is all 0. add prize per square meter

cvFactor = 0.3;
runs = 10;
threshold=-0.5;

for i = 1:runs

	[Xt, Xcv, idxt, idxcv] = splitTrainingData(X, cvFactor);

	yt = y(idxt);
	ycv = y(idxcv);

	mu_t = mean(Xt);
	[D, W_lda] = lda(Xt,yt);
	Xm = bsxfun(@minus, Xt, mu_t);

	% project on feature 1
	Xprojt = project(Xt, W_lda(:, 1));
	Xprojcv = project(Xcv, W_lda(:, 1)); 

	pred = double(Xprojt(:, 1) >= threshold);

	% actually we're interested in detecting crooks, which are flagged as 0 now, so we invert it (positive=crook, false positive=labelled as crook, but is none)
	fprintf("\ntrain\n");
	fprintf('Accuracy: %f\n', mean(pred == yt) * 100);
	[precision recall] = evalPrecisionRecall(idxt, (Xprojt(:, 1) >= threshold) == 0, yt==0, 0);	
	fprintf("crossval\n");
	[precision recall] = evalPrecisionRecall(idxcv, (Xprojcv(:, 1) >= threshold) == 0, ycv==0, 0);

end

scatterPlot1D(Xprojcv, ycv);
