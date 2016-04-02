% linear discriminant analysis like done here gives about 95% precision and 79% recall on training data

addpath("..");
addpath("../pca");

LDA; % load functions in script file. All variables in a script file are in global scope!

[ids, status, X, y] = sanitize(load('../AdFeatures.csv'));
X = [X(:,1:7) (X(:,1) ./ X(:,2))]; % 8th fature is all 0. add prize per square meter

mu = mean(X);
[D, Veig] = lda(X,y);
Veig = real(Veig); % strangely Veig comes with irrational numbers, real part is ok though

Xm = bsxfun(@minus, X, mu);
Xproj = project(Xm, Veig(:, 1:2)); % project on feature 1 and 2

% scale and move
Xproj (:, 1) = Xproj(:, 1) * mu(1) + mu(1);
Xproj (:, 2) = Xproj(:, 2) * mu(2) + mu(2);

cp0 = Xproj(find(y==0),:); % 0 are the bad ones
cp1 = Xproj(find(y==1),:);

figure;
plot(cp0(:,1), cp0(:,2),"ro", "markersize", 10, "linewidth", 3); hold on;
plot(cp1(:,1), cp1(:,2),"go", "markersize", 10, "linewidth", 3);

title("Projection first 2 dimensions\n Eigenvectors of dim1: green dim2: blue", "fontsize", 12);

% print eigenvectors of first 2 components
scale = 100000; 
V = Veig .* scale;
% line([x1 x2], [y1 y2])
scale = 100;
d11 = line([mu(1), mu(1)+V(1,1)], [mu(2), mu(2)+V(1,2)]);
d12 = line([mu(1), mu(1)-V(1,1)], [mu(2), mu(2)-V(1,2)]);
d21 = line([mu(1), mu(1)+V(2,1)], [mu(2), mu(2)+V(2,2)]);
d22 = line([mu(1), mu(1)-V(2,1)], [mu(2), mu(2)-V(2,2)]);

set(d11, 'color', [0 1 0]);
set(d12, 'color', [0 1 0]);
set(d21, 'color', [0 0 1]);
set(d22, 'color', [0 0 1]);

% projected on feature 1 separates ok (try also Xproj(:, 1))

Xproj = project(Xm, Veig(:, 1)); % project on feature 1 only

cp0 = Xproj(find(y==0),:); % 0 are the bad ones
cp1 = Xproj(find(y==1),:);

figure; 
plot(cp0(:,1), 0,"ro", "markersize", 10, "linewidth", 3); hold on;
plot(cp1(:,1), 1,"go", "markersize", 10, "linewidth", 3); 
title("projection 1st dimension");

% actually we're interested in detecting crooks, which are flagged as 0 now, so we invert it (positive=crook, false positive=labelled as crook, but is none)
[precision recall] = evalPrecisionRecall(ids, (Xproj(:, 1) >= -0.5) == 0, y==0, 1);



% projected on 3 features

Xproj = project(Xm, Veig(:, 1:3)); 

% scale and move. 3rd axis has values > 1 now, guess as a result of dim reduction/projection

Xproj (:, 1) = Xproj(:, 1) * mu(1) + mu(1);
Xproj (:, 2) = Xproj(:, 2) * mu(2) + mu(2);
Xproj (:, 3) = Xproj(:, 3) * mu(3) + mu(3);

xx = real(Xproj(:,1));
yy = real(Xproj(:,2));
zz = real(Xproj(:,3));
sizes = ((y==0) + 1) .* 5; 
colors = [y==0 y==1 ((1:size(y)(1)).*0)']; %rgb vector. use zz(:) as last parameter to color acc. to z values

figure; 
scatter3 (xx(:), yy(:), zz(:), 4, colors); 


