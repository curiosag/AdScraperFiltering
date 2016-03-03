%% Initialization
%clear ; close all; clc
lambda = 0.25;
maxOptIter=400;
detectionThreshold = 0.25;

showFalsePositives = 1;

data = load('AdFeatures.csv');
ids = data(:,1);
status = data(:,2);

cols = textread('AdFeatures.col', "%s");

% use all terms (min occ 10)			99.0		but too biased on test set
% raw input, per word feature			96.89
% accuracy with aggregated word types	92.43
% with derived prize/m2 				91.3
% replacing raw facts with derived ones 80
% adding polynomials  					60

idxPrize = 1;
idxSize = 2;

y = status > 0; 
X = data(:, 3:size(data,2));

cols = ['INTERCEPT'; cols(3:length(cols)) ];

m = size(data, 1);
X_norm = [ones(m,1) X]; %featureNormalize(X) ... not possible on 0/1 flags
n = size(X_norm, 2);


shuffle = randperm(m); % 1:m; %
numTrain = idivide (m, 3, "fix") * 2;
fprintf('Using: %d of %d for training\n', numTrain, m);

indices_train = shuffle(1:numTrain);
indices_test = shuffle(numTrain + 1:length(shuffle));

X_train = X_norm(indices_train, :);
X_test =  X_norm(indices_test, :);
y_train = y(indices_train, :);
y_test =  y(indices_test, :);

pos = (y == 1);
neg = (y == 0);
% maybe plot a bit
% plot(X(pos, 1), 'r+','LineWidth', 2, 'MarkerSize', 7)

% Train
initial_theta = zeros(n, 1);
options = optimset('GradObj', 'on', 'MaxIter', maxOptIter);
[theta, J, exit_flag] = ...
	fminunc(@(t)(costFunctionReg(t, X_train, y_train, lambda)), initial_theta, options);

% Compute accuracy on our training set
p_train = predict(theta, X_train, detectionThreshold);
p_test = predict(theta, X_test, detectionThreshold);

fprintf('Train Accuracy: %f\n', mean(double(p_train == y_train)) * 100);
fprintf('test Accuracy: %f\n', mean(double(p_test == y_test)) * 100);

% actually we're interested in detecting crooks, which are flagged as 0 now, so we invert it
fprintf('\nPrecision/Recall training\n');
evalPrecisionRecall(ids(indices_train, :), p_train == 0, y_train == 0, showFalsePositives);
fprintf('\nPrecision/Recall test\n');
evalPrecisionRecall(ids(indices_test, :), p_test == 0, y_test == 0, showFalsePositives);

%printParams(theta, cols, 0, 1);

