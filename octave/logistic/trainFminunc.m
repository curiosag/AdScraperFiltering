function [theta, J, exit_flag] = trainFminunc(X, y, lambda, maxIterations)
	initial_theta = zeros(size(X, 2), 1);
	options = optimset('GradObj', 'on', 'MaxIter', maxIterations);
	[theta, J, exit_flag] = ...
		fminunc(@(t)(costFunctionReg(t, X, y, lambda)), initial_theta, options);
end

