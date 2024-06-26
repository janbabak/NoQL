ALTER TABLE `fit_wiki`
    DROP FOREIGN KEY `fit_wiki_ibfk_1`,
    DROP FOREIGN KEY `fit_wiki_ibfk_2`;

-- command separator
ALTER TABLE `exam`
    DROP FOREIGN KEY `exam_ibfk_1`;

-- command separator
DROP TABLE IF EXISTS `order`;

-- command separator
DROP TABLE IF EXISTS `address`;

-- command separator
DROP TABLE IF EXISTS `user`;

-- command separator
DROP TABLE IF EXISTS `stock_prices`;

-- command separator
DROP TABLE IF EXISTS `student`;

-- command separator
DROP TABLE IF EXISTS `specialisation`;

-- command separator
DROP TABLE IF EXISTS `fit_wiki`;

-- command separator
DROP TABLE IF EXISTS `exam`;

-- command separator
DROP TABLE IF EXISTS `course`;