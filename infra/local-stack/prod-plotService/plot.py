import matplotlib.pyplot as plt
import psycopg2
import numpy as np

# Connect to the database
connection = psycopg2.connect(
    host='host.docker.internal',
    port=5434,
    user='postgres',
    password='123456',
    database='postgres'
)

# Execute the query
query = '''
SELECT l.name, COUNT(f.film_id) AS number_of_movies
FROM film f
JOIN language l ON f.language_id = l.language_id
GROUP BY l.name
ORDER BY number_of_movies DESC
'''

cursor = connection.cursor()
cursor.execute(query)
result = cursor.fetchall()
languages, movie_counts = zip(*result)

# Close the connection
cursor.close()
connection.close()

# Plot
fig, ax = plt.subplots()
ax.bar(languages, movie_counts, color='skyblue')
ax.set_xlabel('Language')
ax.set_ylabel('Number of Movies')
ax.set_title('Number of Movies in Each Language')
plt.xticks(rotation=45, ha='right')
plt.tight_layout()

# Save the plot
plt.savefig('./plotService/plots/ed25f223-072b-4778-b78d-9945f1527db7--549c559d-5130-4729-a18c-9077aa6d41ac.png')
plt.close()