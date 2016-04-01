function Xd = addDerivedFeatures(X, prize, siz)
	Xd = [X (prize ./ siz)];
end
