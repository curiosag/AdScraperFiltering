function plotAds()

figure; hold on;

[ids, status, X, y] = sanitize(load('AdFeatures.csv'));

pos = find(y==1); neg = find(y == 0);

prize = X(:, 1);
siz = X(:, 2);
rooms = X(:, 3);

plot(siz, rooms, 'r+','LineWidth', 2, 'MarkerSize', 7);



%plot(siz(pos, 1), prize(pos, 1), 'ko', 'MarkerFaceColor', 'y', 'MarkerSize', 7);
%plot(siz(neg, 1), prize(neg, 1), 'r+', 'MarkerFaceColor', 'y', 'MarkerSize', 7);

dispData = [X_norm * theta X_norm(:, 2) y];
figure; hold on;
plot(dispData(y == 1, 2), dispData(y == 1, 1), 'ko','LineWidth', 2, 'MarkerSize', 7);
plot(dispData(y == 0, 2), dispData(y == 0, 1), 'r+','LineWidth', 2, 'MarkerSize', 7);
hold off;

end
