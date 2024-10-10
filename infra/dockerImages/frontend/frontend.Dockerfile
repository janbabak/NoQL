FROM node:22.0.0-alpine

WORKDIR /app

COPY ../../../frontend/NoQL/package.json /app/package.json

RUN npm install

COPY ../../../frontend/NoQL /app
COPY ../../../frontend/NoQL/.env.prod /app/.env.prod

RUN npm install -g serve
RUN npm run build:prod

EXPOSE 3000

CMD [ "serve", "-s", "dist" ]