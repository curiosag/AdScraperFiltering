# model evaluation with R
# suggests not to use a prize/size ratio and to exclude phone and provision
# though I think, phone may be ok seeing a bigger sample
# differences are small though and multi-collinearity isn't an issue here

ads_raw=read.table ("AdFeatures.csv" , header=T, sep="," )
ads_raw <- ads_raw[,c(2,4,5,6,7,8,9,10)] #only relevant columns
ads <- subset(ads_raw, size > 10 & size < 150 & prize > 0 & prize < 1500)

ads_reduced <- ads[, c(1,2,3,5,6,8)]

fix(ads)

ads_psratio <- ads
ads_psratio$ratio <- ads$prize/ads$size # add prize/size ratio to dataframe

fix(ads_psratio)
summary(ads_psratio)
names(ads_psratio)
cor(ads_psratio) # correlation amongst all variables. strongest is status-ratio, which later is flagged irrelevant on p-values

plot ( ads$prize , ads$size )
summary(ads)
summary(ads$prize)

mean(ads$prize)
sqrt(var(ads$prize)) # variance, 

var(ads$prize)/m # standard error of mean

# RSS residual sum of squares
# RSE residual standard error, estimate for variance of distribution from a sample
cor(ads)
glm.fit = glm(status~., data=ads) 
glm.fit_psratio = glm(status~., data=ads_psratio) 
glm.fit_reduced= glm(status~., data=ads_reduced)

# AIC Akaike's An Information Criterion, the smaller the better. https://stat.ethz.ch/R-manual/R-devel/library/stats/html/AIC.html
# residual deviance isn't supposed to say much

summary(glm.fit) # AIC 921, residual deviance 205.5 phone and provision seem doubtful
summary(glm.fit_psratio) # AIC: 923 large p-value of ratio (0.9) says, it is not indicative. increases p-value of size and prize though
summary(glm.fit_reduced) # AIC: 919.08 residual deviance 206 here all p-values are low. Seems to be the best choice here

library (car)
vif(glm.fit) # multi-collinearity isn't an issue here, all values way below 5 or 10


anova(glm.fit, glm.fit_psratio, glm.fit_reduced) # anova doesen't say much, dispersion almost unchanged
coef(glm.fit)

# p. 170 ff shows hot to use separate train and cross validation sets and compare results