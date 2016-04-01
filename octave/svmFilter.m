%% Initialization
clear ; close all; clc

% raw input, per word feature			96.89
% accuracy with aggregated word types	92.43
% with derived prize/m2 				91.3
% replacing raw facts with derived ones 80
% adding polynomials  					60

% svm gaussian							67.35
% svm linear							67.35
% won't change at all with whatever settin?

data = load('AdFeatures.csv');

data_filtered = data(and(data(:, 4) > 0, data(:, 2) != 0), :); % filter zero prizes and status != 0 (only classified ads)
ids = data_filtered(:,1);
status = data_filtered(:,2);
idxPrize = 1;
idxSize = 2;

y = status > 0; 
X = data_filtered(:, 4:5); % 4:10 11th coll is all 0, no intercept
X = featureNormalize01(X); % but normalisation for svm

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
evalPrecisionRecall(ids, p == 0, y == 0, showFalsePositives);
