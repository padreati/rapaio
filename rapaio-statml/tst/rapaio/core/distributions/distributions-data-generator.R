
# standard_normal.csv

df <- data.frame(x = seq(-40, 40, 0.01))

for(i in 1:nrow(df)) {
  df$pdf[i] <- dnorm(df$x[i])
  df$cdf[i] <- pnorm(df$x[i])
  df$quantile[i] <- qnorm(df$x[i])
}


write.table(format(df, digits = 15), file = "standard_normal.csv", col.names = TRUE, row.names = FALSE, sep = ",", quote = FALSE)

# order_normal.csv

df <- data.frame(x = seq(-30, 100, 0.01))

for(i in 1:nrow(df)) {
  df$pdf[i] <- dnorm(df$x[i], mean = 10, sd = 2)
  df$cdf[i] <- pnorm(df$x[i], mean = 10, sd = 2)
  df$quantile[i] <- qnorm(df$x[i], mean = 10, sd = 2)
}


write.table(format(df, digits = 15), file = "other_normal.csv", col.names = TRUE, row.names = FALSE, sep = ",", quote = FALSE)

# student t

x <- seq(0,1,0.01)
y <- qt(x, df = 1)
plot(x, y)

df <- data.frame(x = seq(-40, 100, 0.01))

for(i in 1:nrow(df)) {
  df$pdf_1[i] <- dt(df$x[i], df = 1)
  df$cdf_1[i] <- pt(df$x[i], df = 1)
  df$quantile_1[i] <- qt(df$x[i], df = 1)
  
  df$pdf_2[i] <- dt(df$x[i], df = 2)
  df$cdf_2[i] <- pt(df$x[i], df = 2)
  df$quantile_2[i] <- qt(df$x[i], df = 2)
  
  df$pdf_5[i] <- dt(df$x[i], df = 5)
  df$cdf_5[i] <- pt(df$x[i], df = 5)
  df$quantile_5[i] <- qt(df$x[i], df = 5)
  
  df$pdf_10[i] <- dt(df$x[i], df = 10)
  df$cdf_10[i] <- pt(df$x[i], df = 10)
  df$quantile_10[i] <- qt(df$x[i], df = 10)
  
  df$pdf_100[i] <- dt(df$x[i], df = 100)
  df$cdf_100[i] <- pt(df$x[i], df = 100)
  df$quantile_100[i] <- qt(df$x[i], df = 100)
}

write.table(format(df, digits = 15), file = "student.csv", col.names = TRUE, row.names = FALSE, sep = ",", quote = FALSE)


# chi square

df <- data.frame(x = seq(0, 100, 0.01))

for(i in 1:nrow(df)) {
  df$pdf_1[i] <- dchisq(df$x[i], df = 1)
  df$cdf_1[i] <- pchisq(df$x[i], df = 1)
  df$quantile_1[i] <- qchisq(df$x[i], df = 1)
  
  df$pdf_2[i] <- dchisq(df$x[i], df = 2)
  df$cdf_2[i] <- pchisq(df$x[i], df = 2)
  df$quantile_2[i] <- qchisq(df$x[i], df = 2)
  
  df$pdf_5[i] <- dchisq(df$x[i], df = 5)
  df$cdf_5[i] <- pchisq(df$x[i], df = 5)
  df$quantile_5[i] <- qchisq(df$x[i], df = 5)
  
  df$pdf_10[i] <- dchisq(df$x[i], df = 10)
  df$cdf_10[i] <- pchisq(df$x[i], df = 10)
  df$quantile_10[i] <- qchisq(df$x[i], df = 10)
  
  df$pdf_100[i] <- dchisq(df$x[i], df = 100)
  df$cdf_100[i] <- pchisq(df$x[i], df = 100)
  df$quantile_100[i] <- qchisq(df$x[i], df = 100)
}

write.table(format(df, digits=15), file = "chisq.csv", col.names = TRUE, row.names = FALSE, sep = ",", quote = FALSE)

# binomial

df <- data.frame(x = seq(0, 200, 0.01))

df$pdf_10_0.1 <- dbinom(df$x, size = 10, prob = 0.1)
df$cdf_10_0.1 <- pbinom(df$x, size = 10, prob = 0.1)
df$q_10_0.1 <- qbinom(df$x, size = 10, prob = 0.1)

df$pdf_100_0.1 <- dbinom(df$x, size = 100, prob = 0.1)
df$cdf_100_0.1 <- pbinom(df$x, size = 100, prob = 0.1)
df$q_100_0.1 <- qbinom(df$x, size = 100, prob = 0.1)

df$pdf_100_0.9 <- dbinom(df$x, size = 100, prob = 0.9)
df$cdf_100_0.9 <- pbinom(df$x, size = 100, prob = 0.9)
df$q_100_0.9 <- qbinom(df$x, size = 100, prob = 0.9)

df$pdf_2000_0.9 <- dbinom(df$x, size = 2000, prob = 0.9)
df$cdf_2000_0.9 <- pbinom(df$x, size = 2000, prob = 0.9)
df$q_2000_0.9 <- qbinom(df$x, size = 2000, prob = 0.9)

write.table(format(df, digits=20), file = "binom.csv", col.names = TRUE, row.names = FALSE, sep = ",", quote = FALSE)

# hypergeometric

df <- data.frame(x = seq(0, 200, 0.01))

df$pdf_20_20_30 <- dhyper(df$x, 20, 20, 30)
df$cdf_20_20_30 <- phyper(df$x, 20, 20, 30)
df$q_20_20_30 <- qhyper(df$x, 20, 20, 30)

df$pdf_70_70_100 <- dhyper(df$x, 70, 70, 100)
df$cdf_70_70_100 <- phyper(df$x, 70, 70, 100)
df$q_70_70_100 <- qhyper(df$x, 70, 70, 100)

write.table(format(df, digits=20), file = "hyper.csv", col.names = TRUE, row.names = FALSE, sep = ",", quote = FALSE)

# poisson

df <- data.frame(x = seq(0, 100, 0.01))

df$pdf_1 <- dpois(df$x, 1)
df$cdf_1 <- ppois(df$x, 1)
df$q_1 <- qpois(df$x, 1)

df$pdf_5 <- dpois(df$x, 5)
df$cdf_5 <- ppois(df$x, 5)
df$q_5 <- qpois(df$x, 5)

df$pdf_10 <- dpois(df$x, 10)
df$cdf_10 <- ppois(df$x, 10)
df$q_10 <- qpois(df$x, 10)

df$pdf_100 <- dpois(df$x, 100)
df$cdf_100 <- ppois(df$x, 100)
df$q_100 <- qpois(df$x, 100)

write.table(format(df, digits=20), file = "pois.csv", col.names = TRUE, row.names = FALSE, sep = ",", quote = FALSE)

# gamma

df <- data.frame(x = seq(0, 100, 0.01))

df$pdf_1 <- dgamma(df$x, shape = 0.5, scale = 0.5)
df$cdf_1 <- pgamma(df$x, shape = 0.5, scale = 0.5)
df$q_1 <- qgamma(df$x, shape = 0.5, scale = 0.5)

df$pdf_2 <- dgamma(df$x, shape = 1, scale = 0.5)
df$cdf_2 <- pgamma(df$x, shape = 1, scale = 0.5)
df$q_2 <- qgamma(df$x, shape = 1, scale = 0.5)

df$pdf_3 <- dgamma(df$x, shape = 5, scale = 0.5)
df$cdf_3 <- pgamma(df$x, shape = 5, scale = 0.5)
df$q_3 <- qgamma(df$x, shape = 5, scale = 0.5)

df$pdf_4 <- dgamma(df$x, shape = 0.5, scale = 1)
df$cdf_4 <- pgamma(df$x, shape = 0.5, scale = 1)
df$q_4 <- qgamma(df$x, shape = 0.5, scale = 1)

df$pdf_5 <- dgamma(df$x, shape = 1, scale = 1)
df$cdf_5 <- pgamma(df$x, shape = 1, scale = 1)
df$q_5 <- qgamma(df$x, shape = 1, scale = 1)

df$pdf_6 <- dgamma(df$x, shape = 5, scale = 1)
df$cdf_6 <- pgamma(df$x, shape = 5, scale = 1)
df$q_6 <- qgamma(df$x, shape = 5, scale = 1)

df$pdf_7 <- dgamma(df$x, shape = 0.5, scale = 5)
df$cdf_7 <- pgamma(df$x, shape = 0.5, scale = 5)
df$q_7 <- qgamma(df$x, shape = 0.5, scale = 5)

df$pdf_8 <- dgamma(df$x, shape = 1, scale = 5)
df$cdf_8 <- pgamma(df$x, shape = 1, scale = 5)
df$q_8 <- qgamma(df$x, shape = 1, scale = 5)

df$pdf_9 <- dgamma(df$x, shape = 5, scale = 5)
df$cdf_9 <- pgamma(df$x, shape = 5, scale = 5)
df$q_9 <- qgamma(df$x, shape = 5, scale = 5)

write.table(format(df, digits=20), file = "gamma.csv", col.names = TRUE, row.names = FALSE, sep = ",", quote = FALSE)
