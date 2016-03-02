
%% Initialization
clear ; close all; clc

data = load('AdFeatures.csv');

status = data(:,2);
prize = data(:,3);
size= data(:,4);
rooms = data(:,5);
hasEmail = data(:,6);
factorPositiveTerms = data(:,7);
factorNegativeTerms = data(:,8);
factorProfessionalTerms = data(:,9);
substandard = data(:,10);
provision = data(:,11);
kaution = data(:,12);

% accuracy with raw input is 			92.43
% with derived prize/m2 				91.3
% adding polynomials  					60
% replacing raw facts with derived ones 80

% hardFacts = (provision + kaution) / 2 - hasEmail;
% softFacts = factorPositiveTerms + factorProfessionalTerms - factorNegativeTerms;
% combinedFacts = ppm2 .* hardFacts .* softFacts;

features = [prize,size,rooms,substandard, provision, kaution, hasEmail, factorPositiveTerms, factorNegativeTerms, factorProfessionalTerms];

m = length(status);
n = 10;
y = status > 0; 
X = features(size > 0, :); % remove unlikely outliers
X_norm = [ones(m,1) featureNormalize(X)];

pos = (y == 1);
neg = (y == 0);
% maybe plot a bit
% plot(X(pos, 1), 'r+','LineWidth', 2, 'MarkerSize', 7)

% Optimize

initial_theta = zeros(n, 1);
lambda = 1;
options = optimset('GradObj', 'on', 'MaxIter', 400);
[theta, J, exit_flag] = ...
	fminunc(@(t)(costFunctionReg(t, X, y, lambda)), initial_theta, options);

% Compute accuracy on our training set
p = predict(theta, X);
fprintf('Train Accuracy: %f\n', mean(double(p == y)) * 100);



