% from mooc ml ex7

function [U, S, X_norm, mu, sigma] = PCA(X)
%PCA Run principal component analysis on the dataset X
%   [U, S, X] = pca(X) computes eigenvectors of the covariance matrix of X
%   Returns the eigenvectors U, the eigenvalues (on diagonal) in S
%

% normalize each feature to mean value of 0 

mu = mean(X);
X_norm = bsxfun(@minus, X, mu);

% no standardisation to standard deviation of 1 to stay consistent with the LDA stuff
% sigma = std(X_norm);
% X_norm = bsxfun(@rdivide, X_norm, sigma);

% Useful values
[m, n] = size(X);

% You need to return the following variables correctly.
U = zeros(n);
S = zeros(n);

% ====================== YOUR CODE HERE ======================
% Instructions: You should first compute the covariance matrix. Then, you
%               should use the "svd" function to compute the eigenvectors
%               and eigenvalues of the covariance matrix. 
%
% Note: When computing the covariance matrix, remember to divide by m (the
%       number of examples).
%


sigma = (X_norm' * X_norm) / m; % covariance_matrix (without mean normalization)

[U, S, V] = svd(sigma);


% =========================================================================

end
