%% Initialization
%clear ; close all; clc

data = load('AdFeatures.csv');
cols = textread('AdFeatures.col', "%s");
status = data(:,2);

% use all terms (min occ 10)			99.0
% raw input, per word feature			96.89
% accuracy with aggregated word types	92.43
% with derived prize/m2 				91.3
% replacing raw facts with derived ones 80
% adding polynomials  					60

m = size(data)(1);
n_data = size(data)(2);
features = data(:, 3:n_data);
n = n_data - 2;

IdxSize = 2;
idxSubstandard = 8;
features(:,idxSubstandard) = features(:,idxSubstandard) .* 10; % boost credibility of substandard indicator

y = status > 0; 
X = features; %features(features(:, IdxSize) > 0, :);  remove unlikely outliers
X_norm = [ones(m,1) featureNormalize(X)];

shuffle = randperm(m);
numTrain = idivide (m, 3, "fix") * 2;
fprintf('Using: %d of %d for training\n', numTrain, m);

indices_train = shuffle(1:numTrain);
indices_test = shuffle(numTrain + 1:length(shuffle));

X_train = X_norm(indices_train, :);
X_test =  X_norm(indices_test, :);
y_train = y(indices_train);
y_test =  y(indices_test);

n = n + 1;
pos = (y == 1);
neg = (y == 0);
% maybe plot a bit
% plot(X(pos, 1), 'r+','LineWidth', 2, 'MarkerSize', 7)

% Optimize
initial_theta = zeros(n, 1);
lambda = 1;
options = optimset('GradObj', 'on', 'MaxIter', 400);
[theta, J, exit_flag] = ...
	fminunc(@(t)(costFunctionReg(t, X_train, y_train, lambda)), initial_theta, options);

detectionThreshold = 0.2;
% Compute accuracy on our training set
p_train = predict(theta, X_train, detectionThreshold);
p_test = predict(theta, X_test, detectionThreshold);

fprintf('Train Accuracy: %f\n', mean(double(p_train == y_train)) * 100);
fprintf('test Accuracy: %f\n', mean(double(p_test == y_test)) * 100);


% actually we're interested in detecting crooks, which are flagged as 0, so we invert it
fprintf('\nPrecision/Recall training\n');
evalPrecisionRecall(p_train == 0, y_train == 0);
fprintf('\nPrecision/Recall test\n');
evalPrecisionRecall(p_test == 0, y_test == 0);


%printParams();

