
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
