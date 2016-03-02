%% Initialization
clear ; close all; clc

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

shuffle = randperm(m);

IdxSize = 2;
idxSubstandard = 8;
features(:,idxSubstandard) = features(:,idxSubstandard) .* 10; % boost credibility of substandard indicator

y = status > 0; 
X = features; %features(features(:, IdxSize) > 0, :);  remove unlikely outliers
X_norm = [ones(m,1) featureNormalize(X)];





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
	fminunc(@(t)(costFunctionReg(t, X_norm, y, lambda)), initial_theta, options);

% Compute accuracy on our training set
p = predict(theta, X_norm);
fprintf('Train Accuracy: %f\n', mean(double(p == y)) * 100);

printParams();

