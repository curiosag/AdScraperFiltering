% linear discriminant analysis like done here gives about 95% precision and 79% recall on training data

addpath("..");
addpath("../pca");

LDA; % load functions in script file. All variables in a script file are in global scope!

[ids, status, X, y] = sanitize(load('../AdFeatures.csv'));
X = [X(:,1:7) (X(:,1) ./ X(:,2))]; % 8th fature is all 0. add prize per square meter

mu_total = mean(X);
[D, W_lda] = lda(X,y);
Xm = bsxfun(@minus, X, mu_total);
Xproj = project(Xm, W_lda(:, 1:2)); % project on feature 1 and 2

% scale and move

Xproj (:, 1) = Xproj(:, 1) * mu_total(1) + mu_total(1);
Xproj (:, 2) = Xproj(:, 2) * mu_total(2) + mu_total(2);

cp0 = Xproj(find(y==0),:); % 0 are the bad ones
cp1 = Xproj(find(y==1),:);

figure;
mu = mu_total;
plot(cp0(:,1), cp0(:,2),"ro", "markersize", 10, "linewidth", 3); hold on;
plot(cp1(:,1), cp1(:,2),"go", "markersize", 10, "linewidth", 3);
title("projection 2 dimensions");

% print eigenvectors of first 2 components, almost the same acutally
% V=W_lda;

figure;
 scale = 3000; 
 offset = 0; 
 Vs = V .* scale + offset;

Vs(1:2,1:2)

drawLine([Vs(1,1) Vs(1,1)], [Vs(2,1) Vs(2,1)], '-k', 'LineWidth', 2);
drawLine([Vs(1,2) Vs(1,2)], [Vs(2,2) Vs(2,2)], '-k', 'LineWidth', 2);



% actually we're interested in detecting crooks, which are flagged as 0 now, so we invert it (positive=crook, false positive=labelled as crook, but is none)
evalPrecisionRecall(ids, (Xproj(:, 1) >= -0.5) == 0, y==0, 1);

% projected on feature 1 it separates ok

Xproj = project(Xm, W_lda(:, 1)); % project on feature 1 only

cp0 = Xproj(find(y==0),:); % 0 are the bad ones
cp1 = Xproj(find(y==1),:);

figure; 
plot(cp0(:,1), 0,"ro", "markersize", 10, "linewidth", 3); hold on;
plot(cp1(:,1), 1,"go", "markersize", 10, "linewidth", 3); 
title("projection 1st dimension");

% projected on 3 features

Xproj = project(Xm, W_lda(:, 1:3)); % project on 3 features

% scale and move. 3rd axis has values > 1 now, guess as a result of dim reduction/projection

Xproj (:, 1) = Xproj(:, 1) * mu_total(1) + mu_total(1);
Xproj (:, 2) = Xproj(:, 2) * mu_total(2) + mu_total(2);
Xproj (:, 3) = Xproj(:, 3) * mu_total(3) + mu_total(3);

xx = real(Xproj(:,1));
yy = real(Xproj(:,2));
zz = real(Xproj(:,3));
sizes = ((y==0) + 1) .* 5; 
colors = [y==0 y==1 ((1:size(y)(1)).*0)']; %rgb vector. use zz(:) as last parameter to color acc. to z values

figure; 
scatter3 (xx(:), yy(:), zz(:), 4, colors); 


