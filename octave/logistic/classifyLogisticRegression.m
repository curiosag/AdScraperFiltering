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
% adding price per square meter			85

% use only size, prize, rooms			75
% use size, prize, rooms, hasMail		90

% accuracy is not great with this model (85%) but precision is >= 95% leaving a recall of about 60-65%
% strangely removal of ppm2 may improve things
% precision preferrable to LDA approach, though overall performance is worse

%% Initialization
%clear ; close all; clc

verbose = 1;
lambda = 0.25;
maxIterations=400;
detectionThreshold = 0.1;

[ids, status, X, y] = sanitize(load('AdFeatures.csv'));
X = [X(:,1:8) (X(:,1) ./ X(:,2))];

cols = textread('AdFeatures.col', "%s");
cols = ['INTERCEPT'; cols(4:11) ];%length(cols)

cols = [cols; "ppm2";];

m = size(X, 1);
X_norm = [ones(m,1) X]; %featureNormalize(X) ... not possible on 0/1 flags
n = size(X_norm, 2);

[theta, J, exit_flag] = trainFminunc(X_norm, y, lambda, maxIterations);

p = predict(theta, X_norm, detectionThreshold);

if verbose
	showFalsePositives = 1;

	fprintf('Train Accuracy: %f\n', mean(double(p == y)) * 100);

	% actually we're interested in detecting crooks, which are flagged as 0 now, so we invert it
	% (positive=crook, false positive=labelled as crook, but is none)

	fprintf('\nPrecision/Recall training\n');
	evalPrecisionRecall(ids, p == 0, y == 0, showFalsePositives);
	
	printParams(theta, cols(1:10), 0, 1, 1);

endif

