% experiments with size, price and size/prize ratio only
% adding rooms doesen't make much of a difference

%% Initialization
%clear ; close all; clc

classificationThreshold = 0.20; % sigmoid >= this is an ok item
lambda = 0.2;


data = load('AdFeatures.csv');
cols = textread('AdFeatures.col', "%s");
ids = data(:,1);
status = data(:,2);

m = size(data)(1);

idxPrize = 1;
idxSize = 2;

y = status > 0; 
X = data(:, 3:5); 

% using price per square meter instead of price and size separately
% brings down accuracy by 1% and precision even more
ppm2 = X(:, idxSize) ./ X(:, idxPrize);

% introducing a distance of ppm2 to the average ppm2 of the 30% with highest values
% doesent make a change at all
fractionsAvg = 3;
mAvg = idivide(m, fractionsAvg);
top30 = sort(ppm2)(2*mAvg:m, :);
ppm2Top30Average = sum(top30)/size(top30)(1);
ppm2TopDist = ppm2 - ppm2Average;

% using polynomials sets all parameters to zero??
polynomials = mapFeature(X(:, 1), X(:, 2));
n_polynomials = size(polynomials, 2);

X = [ X ]; % ppm2
cols = ['INTERCEPT'; 'prize'; 'size'; 'rooms'; ]; %'ppm2' % num2cell((1:n_polynomials)')

X_norm = [ones(m,1) featureNormalize(X)];
n = size(X_norm)(2);

shuffle = randperm(m);
%shuffle = 1:m;


numTrain = idivide (m, 5, "fix") * 4;
fprintf('Using: %d of %d for training\n\n', numTrain, m);

indices_train = shuffle(1:numTrain);
indices_test = shuffle(numTrain + 1:length(shuffle));

X_train = X_norm(indices_train, :);
X_test =  X_norm(indices_test, :);
y_train = y(indices_train);
y_test =  y(indices_test);

pos = (y == 1);
neg = (y == 0);
% maybe plot a bit
% plot(X(pos, 1), 'r+','LineWidth', 2, 'MarkerSize', 7)

% Optimize
initial_theta = zeros(n, 1);
options = optimset('GradObj', 'on', 'MaxIter', 800);
[theta, J, exit_flag] = ...
	fminunc(@(t)(costFunctionReg(t, X_train, y_train, lambda)), initial_theta, options);

% Compute accuracy on our training set
p_train = predict(theta, X_train, classificationThreshold);
p_test = predict(theta, X_test, classificationThreshold);

fprintf('Accuracy\n');
fprintf('\ttrain: \t%.3f\n', mean(double(p_train == y_train)) * 100);
fprintf('\ttest: \t%.3f\n', mean(double(p_test == y_test)) * 100);


% actually we're interested in detecting crooks, which are flagged as 0, so we invert it
showFalsePositives = 0;
fprintf('\np/r Training\n');
evalPrecisionRecall(ids(indices_train, :), p_train == 0, y_train == 0, showFalsePositives);
fprintf('\np/r Test\n');
evalPrecisionRecall(ids(indices_test, :), p_test == 0, y_test == 0, showFalsePositives);

printParams(theta, cols, 0, 1);

