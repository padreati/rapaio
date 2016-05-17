
# standard_normal.csv

df <- data.frame(x = seq(-40, 40, 0.01))

for(i in 1:nrow(df)) {
  df$pdf[i] <- dnorm(df$x[i])
  df$cdf[i] <- pnorm(df$x[i])
  df$quantile[i] <- qnorm(df$x[i])
}


write.table(format(df, digits = 15), file = "standard_normal.csv", col.names = TRUE, row.names = FALSE, sep = ",", quote = FALSE)
