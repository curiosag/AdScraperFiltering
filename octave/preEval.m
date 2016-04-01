
good = avg(X_norm(y == 1, 1:9)) % the good ones
evil = avg(X_norm(y == 0, 1:9)) % the evil ones

	for i = 1:9
    	fprintf('%s\t%.3f\t%.3f\n', cols{i}, good(i), evil(i));
	end





%	@	-@
%1 	10	194	0.034
%-1	125	20	0.862
