% PCA is pretty messy applied to this

addpath("..");
[ids, status, X, y] = sanitize(load('../AdFeatures.csv'));
X = [X(:,1:7) (X(:,1) ./ X(:,2))]; % 8th fature is all 0. add prize per square meter

[U, S, X_norm, mu, sigma] = PCA(X);

%  Draw the eigenvectors centered at mean of data. These lines show the
%  directions of maximum variations in the dataset.

drawLine(mu, mu + 1.5 * S(1,1) * U(:,1)', '-k', 'LineWidth', 2);
drawLine(mu, mu + 1.5 * S(2,2) * U(:,2)', '-k', 'LineWidth', 2);
title("eigenvectors");

fprintf('Top eigenvector: \n');
fprintf(' U(:,1) = %f %f \n', U(1,1), U(2,1));

%  Project the data onto K = 2 dimension
K = 2;
Z2 = projectData(X_norm, U, K);

cp0 = Z2(find(y==0),:); % 0 are the bad ones
cp1 = Z2(find(y==1),:);

figure; % with size as X axis
plot(cp0(:,2), cp0(:,1),"ro", "markersize", 10, "linewidth", 3); hold on;
plot(cp1(:,2), cp1(:,1),"go", "markersize", 10, "linewidth", 3);
title("projection 2 dimensions");

%  Project the data onto K = 1 dimension

K = 1;
Z1 = projectData(X_norm, U, K);
cp0 = Z1(find(y==0),:); % 0 are the bad ones
cp1 = Z1(find(y==1),:);

figure; % with size as X axis
plot(cp0(:,1), 0,"ro", "markersize", 10, "linewidth", 3); hold on;
plot(cp1(:,1), 1,"go", "markersize", 10, "linewidth", 3);
title("projection 1 dimension");

