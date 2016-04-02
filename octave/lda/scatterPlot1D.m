function scatterPlot1D (X, y)

	cp0 = X(find(y==0),:); % 0 are the bad ones
	cp1 = X(find(y==1),:);

	figure; 
	plot(cp0(:,1), 0,"ro", "markersize", 10, "linewidth", 3); hold on;
	plot(cp1(:,1), 1,"go", "markersize", 10, "linewidth", 3); 
	title("projection 1st dimension");

end
