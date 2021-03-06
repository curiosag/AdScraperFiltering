addpath("..");

% expected columns in input:
%
% 1 id
% 2 status
% 3 statusPredicted
% 4 prize
% 5 size
% 6 phone
% 7 hasEmail
% 8 substandard
% 9 provision
% 10 kaution
% 11 ablos
% 12 airbnb;      ----> from including this column it is a custom list of terms defined in dictionary
% 13 achtung;
% 14 hotmail.com;
% ...


% use all terms (min occ 10)			99.0		but too biased on test set
% raw input, per word feature			96.89
% accuracy with aggregated word types	92.43
% with derived prize/m2 				91.3
% replacing raw facts with derived ones 80
% adding polynomials  					60

% use only size, prize, rooms			75
% use size, prize, rooms, hasMail		90


%% Initialization
%clear ; close all; clc

verbose = 1;
lambda = 0.25;
maxIterations=400;
detectionThreshold = 0.1;
cvFactor = 0.2; % fraction for cross-validation

[ids, status, X, y] = sanitize(load('AdFeatures.csv'));
X = [X(:,1:8) (X(:,1) ./ X(:,2))]; 

cols = textread('AdFeatures.col', "%s");
cols = ['INTERCEPT'; cols(4:11) ];%length(cols)

cols = [cols; "ppm2";];

m = size(X, 1);
X_norm = [ones(m,1) X]; %featureNormalize(X) ... not possible on 0/1 flags
n = size(X_norm, 2);

[X_train, X_cv, y_train, y_cv, indices_train, indices_cv] = splitTrainingData(X_norm, y, cvFactor);

[theta, J, exit_flag] = trainFminunc(X_train, y_train, lambda, maxIterations);

p = predict(theta, X_norm, detectionThreshold);

if 1
	showFalsePositives = 1;

	p_train = p(indices_train, :);
	p_cv = p(indices_cv, :);

	fprintf('Train Accuracy: %f\n', mean(double(p_train == y_train)) * 100);
	fprintf('test Accuracy: %f\n', mean(double(p_cv == y_cv)) * 100);

	% actually we're interested in detecting crooks, which are flagged as 0 now, so we invert it
	fprintf('\nPrecision/Recall training\n');
	evalPrecisionRecall(ids(indices_train, :), p_train == 0, y_train == 0, showFalsePositives);
	fprintf('\nPrecision/Recall test\n');
	evalPrecisionRecall(ids(indices_cv, :), p_cv == 0, y_cv == 0, showFalsePositives);

	printParams(theta, cols(1:10), 0, 1, 0);

endif

