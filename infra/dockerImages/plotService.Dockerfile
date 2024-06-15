# This image is used by the PlotService in backend to generate the plots in an isolated environment.
# https://github.com/docker-library/python/blob/2d4fb586c48b067b432cf56653ee2541d94fdd7d/3.12/alpine3.20/Dockerfile
FROM python:3.12.4-alpine

RUN apk add build-base # install gcc - necessary for installing packages with C dependencies
RUN pip install \
    matplotlib \
    pandas \
    numpy \
    psycopg2-binary==2.9.9 \
    sqlalchemy

RUN mkdir plotService
