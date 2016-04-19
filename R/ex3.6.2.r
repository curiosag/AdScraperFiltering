# exercises 3.6.2

# library loading as function
LoadLibraries = function(){
  library ( MASS )
  library ( ISLR )
  library (car)
}

LoadLibraries()

fix(Boston)
names(Boston)

lm.fit=lm(medv~lstat, data=Boston);

summary(lm.fit)
names(lm.fit) # what's all there in lm.fit
lm.fit$coefficients # can be done this way
coef(lm.fit) # but the "official" functions are preferred
confint(lm.fit)

plot(Boston[["lstat"]], Boston[["medv"]])
abline(lm.fit) # plot regression line, use parameters for line width and color: lwd =3 , col =" red "

par(mfrow=c(2,2)) # plot simultaneously in a 2 by 2 grid of plots
plot(lm.fit) # you can plot some characterstics this way

plot(predict(lm.fit), residuals(lm.fit)) # plot residuals
plot(predict(lm.fit), rstudent(lm.fit)) # plot studenized residuals

plot (hatvalues(lm.fit)) # leverage statistics
which.max(hatvalues(lm.fit)) # which.max gives max value of a vector

# multiple lin. regression
attach(Boston) # so names of Boston can be used without prefix
lm.fit = lm(medv~lstat+age) # fit with lstat and age
lm.fit = lm(medv~., data=Boston) # fit with all features, data source needs to be explicit here

names(summary(lm.fit))

summary(lm.fit)$r.squared 	#R^2
summary(lm.fit)$sigma 		# RSE

vif(lm.fit)			# variance inflation factors
library(car)
# car isn't there by default and needs some packages to be installed first
R.Version() # is 3.2.2, current pbkrtest needs >= 3.2.3
install.packages("lme4") # seems to be needed first
url<-"https://cran.r-project.org/src/contrib/Archive/pbkrtest/pbkrtest_0.4-4.tar.gz" 
install.packages(url, repos=NULL, type="source")
install.packages("car")

summary(lm.fit)
# age has hight p-value (0.958229), let's try to exclude it, gives little higher f-statistic
# age          6.922e-04  1.321e-02   0.052 0.958229   
lm.fit = lm(medv~.-age, data=Boston)

# add interaction term lstat x age
summary(lm(medv~lstat+age+lstat:age, data=Boston))
summary(lm(medv~lstat*age, data=Boston)) # same as above, short version. adds lstat, age and the interaction term

# add non linear term
lm.fit2 = lm(medv~lstat+I(lstat^2), data=Boston)
summary(lm.fit2)
lm.fit1 = lm(medv~lstat, data=Boston) # let's take a simple linear one
anova(lm.fit1, lm.fit2) # and compare the quadratic model

# Model 1: medv ~ lstat
# Model 2: medv ~ lstat + I(lstat^2)
#   Res.Df   RSS Df Sum of Sq     F    Pr(>F)    
# 1    504 19472                                 
# 2    503 15347  1    4125.1 135.2 < 2.2e-16 ***

# F value of 135 with almost zero Pr(>F) suggests that model 2 is much better

par(mfrow=c(2,2))
plot(lm.fit2)

lm.fit3 = lm(medv~poly(lstat,5), data=Boston) # create fifth order polynomial fit, instead of typing I(X^5) and all lower exponents
summary(lm.fit3)
summary(lm(medv~log(rm), data=Boston)) # or a log transformation

# Carseats dataset to demonstrate dummy variables
fix(Carseats)
lm.fit = lm(Sales~.+Income:Advertising+Price:Age, data=Carseats)
summary(lm.fit)
contrasts(Carseats[["ShelveLoc"]]) # displays dummy variable encoding for ShelveLoc

# exercise applied 8 on Auto dataset
fix(Auto)
names(Auto)
lm.fit = lm(mpg~horsepower, data=Auto) 
summary(lm.fit)
# p-value low, so there is a relationship 
# how strong is it? residual standard error: 4.906, mean of mpg is 23.45 => percentage error is ~20%, so-so
# R^2 static is 0.6, means 60% of variance in mpg is explained by prediction. so-so
# relationship is negative (parameter < 0)
# 95% confint is 23,45 +/-2*4,9

confint(lm.fit)
summary(Auto)

par(mfrow=c(2,2))
plot(lm.fit)
plot(predict(lm.fit), residuals(lm.fit))
abline(lm.fit)

pairs(mpg~., data=Auto) # scatterplot of it all
attach(Auto)
names(Auto)
cor(Auto[,-9]) # exclude 9th col which isn't numeric