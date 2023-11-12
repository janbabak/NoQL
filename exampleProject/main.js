import GptApi from './api.js';
import {DbClient, dbSchema} from "./database.js";

const api = new GptApi();
const dbClient = new DbClient();

const nlpQuery = 'Find all males that are older than 30 years'
const gptQuery =
    'Translate this text into SQL query. ' +
    'The SQL dialect is: Postgres. ' +
    'Surround all table names by quotes' +
    'This is the database schema: ' + dbSchema + ' ' +
    'This is the text that needs to be translated: ' + nlpQuery
const sqlQuery = await api.sendQuery(gptQuery)
console.log('SQL Query: ' + sqlQuery);
const result = await dbClient.executeQuery(sqlQuery)

console.log(result);