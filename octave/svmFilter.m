%% Initialization
clear ; close all; clc

data = load('AdFeatures.csv');

status = data(:,2);

% raw input, per word feature			96.89
% accuracy with aggregated word types	92.43
% with derived prize/m2 				91.3
% replacing raw facts with derived ones 80
% adding polynomials  					60

% svm gaussian							67.35
% svm linear							67.35
% won't change at all with whatever settin?

m = size(data)(1);
n_data = size(data)(2);
features = data(:, 3:n_data);
n = n_data - 2;

IdxSize = 2;
idxSubstandard = 8;
features(:,idxSubstandard) = features(:,idxSubstandard) .* 10; % boost credibility of substandard indicator

y = status > 0; 
X = features; %no incept term, no normalization for svm

pos = (y == 1);
neg = (y == 0);
% maybe plot a bit
% plot(X(pos, 1), 'r+','LineWidth', 2, 'MarkerSize', 7)

% Optimize
C = 0.1;
sigma = 0.1;
model = svmTrain(X, y, C,  @(x1, x2) gaussianKernel(x1, x2, sigma));

p = svmPredict(model, X);

fprintf('Training Accuracy: %f\n', mean(double(p == y)) * 100);

