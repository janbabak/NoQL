import matplotlib.pyplot as plt
import psycopg2

conn = psycopg2.connect(host='https://my-eshop.com', port=5432, user='jan', password='secret111', database='myEshop')
cur = conn.cursor()
cur.execute("SELECT sex, COUNT(*) FROM eshop_user WHERE age > 24 GROUP BY sex")
rows = cur.fetchall()

sex = [row[0] for row in rows]
count = [row[1] for row in rows]

plt.bar(sex, count)
plt.xlabel('Sex')
plt.ylabel('Count')
plt.title('Count of Users by Sex (Age > 24)')
plt.savefig('./plotService/plots/68712114-b7b4-451a-a6eb-1c6e822509ae--12345678-b7b4-451a-a6eb-1c6e822509ae.png')