FROM node:22.0.0-alpine

WORKDIR /app

COPY ../../frontend/NoQL/package.json /app/package.json

RUN npm install

COPY ../../frontend/NoQL /app

RUN npm install -g serve
RUN npm run build

EXPOSE 3000

CMD [ "serve", "-s", "dist" ]