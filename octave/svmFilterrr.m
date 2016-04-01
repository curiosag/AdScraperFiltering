%% Initialization
clear ; close all; clc

% raw input, per word feature			96.89
% accuracy with aggregated word types	92.43
% with derived prize/m2 				91.3
% replacing raw facts with derived ones 80
% adding polynomials  					60

% svm gaussian							67.35
% svm linear							67.35
% p is always only 1es
% won't change at all with whatever setting?
load('ex6data1.mat');

% Plot training data
plotData(X, y);

[m, n] = size(X);

fprintf('Program paused. Press enter to continue.\n');
pause;

pos = (y == 1);
neg = (y == 0);
% maybe plot a bit
% plot(X(pos, 1), 'r+','LineWidth', 2, 'MarkerSize', 7)

% Optimize
C = 0.1;
sigma = 1;
model = svmTrain(X, y, C,  @(x1, x2) linearKernel(x1, x2, sigma), 0.00001, 10);

p = svmPredict(model, X);

fprintf('Training Accuracy: %f\n', mean(double(p == y)) * 100);

showFalsePositives = 0;
evalPrecisionRecall(1:m, p == 0, y == 0, showFalsePositives);
