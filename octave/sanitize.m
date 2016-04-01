function [ids, status, X, y] = sanitize(data)

	% [ids, status, X, y] = sanitize(load('AdFeatures.csv'));

	% expected columns in input:
	%
	% 1 id
	% 2 status
	% 3 statusPredicted
	% 4 prize
	% 5 size
	% 6 rooms
	% 7 hasEmail
	% 8 substandard
	% 9 provision
	% 10 kaution
	% 11 ablos
	% 12 airbnb;      ----> from including this column it is a custom list of terms defined in dictionary
	% 13 achtung;
	% 14 hotmail.com;
	% ...

	nonzeroPrize=data(:, 4) > 0;
	nonzeroStatus= data(:, 2) != 0;
	plausibleSize=data(:, 5) > 10; %seems some confuse it with rooms
	data_filtered = data(and(and(nonzeroPrize, nonzeroStatus), plausibleSize), :); 

	prize = data_filtered(:, 4);
	siz = data_filtered(:, 5);
		
	data_filtered = data_filtered (and (siz < 150, prize < 1500), :); %unlikely outliers

	ids = data_filtered(:,1);
	status = data_filtered(:,2);

	y = status > 0; 
	X = data_filtered(:, 4:size(data_filtered,2));

end
