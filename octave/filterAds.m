
%% Initialization
clear ; close all; clc

data = load('AdFeatures.csv');

status = data(:,2);
ppm2 = data(:,3);
hasEmail = data(:,5);
factorPositiveTerms = data(:,6);
factorNegativeTerms = data(:,7);
factorProfessionalTerms = data(:,8);
substandard = data(:,9);
provision = data(:,10);
kaution = data(:,11);

hardFacts = (provision + kaution) / 2 - hasEmail;
softFacts = factorPositiveTerms + factorProfessionalTerms - factorNegativeTerms;
combinedFacts = ppm2 .* hardFacts .* softFacts;

features = [ppm2, substandard, hardFacts, softFacts, combinedFacts, mapFeature(ppm2, combinedFacts)];

m = length(ppm2);
y = status > 0; 
X = features(ppm2 < 40, :); % remove unlikely outliers
X_norm = [ones(m,1) featureNormalize(X)];

pos = (y == 1);
neg = (y == 0);
% maybe plot a bit
% plot(X(pos, 1), 'r+','LineWidth', 2, 'MarkerSize', 7)


% Optimize

initial_theta = zeros(size(X, 2), 1);
lambda = 1;
options = optimset('GradObj', 'on', 'MaxIter', 400);
[theta, J, exit_flag] = ...
	fminunc(@(t)(costFunctionReg(t, X, y, lambda)), initial_theta, options);

% Compute accuracy on our training set
p = predict(theta, X);
fprintf('Train Accuracy: %f\n', mean(double(p == y)) * 100);

