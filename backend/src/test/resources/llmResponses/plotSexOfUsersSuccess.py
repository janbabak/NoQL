import matplotlib.pyplot as plt
import psycopg2

conn = psycopg2.connect(host='localhost', port=1111111111, user='admin4445900234', password='dkl45349?405', database='database99889899')
cur = conn.cursor()
cur.execute("SELECT sex, COUNT(*) FROM eshop_user WHERE age > 24 GROUP BY sex")
rows = cur.fetchall()

sex = [row[0] for row in rows]
count = [row[1] for row in rows]

plt.bar(sex, count)
plt.xlabel('Sex')
plt.ylabel('Count')
plt.title('Count of Users by Sex (Age > 24)')
plt.savefig('./plotService/plots/noQlGeneratedPlot.png')