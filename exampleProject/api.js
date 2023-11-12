import fetch from 'node-fetch';
import dotenv from 'dotenv';

dotenv.config();

class GptApi {
    #url = 'https://api.openai.com/v1/chat/completions'
    #apiKey = process.env.API_KEY

    constructor(gptModel = 'gpt-3.5-turbo') {
        this.gptModel = gptModel
    }

    async sendQuery(query) {
        const response = await fetch(this.#url, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + this.#apiKey,
                'Content-Type': 'application/json'
            },
            body: this.#createBody(query)
        })
        if (response.ok) {
            try {
                const data = await response.json()
                return data.choices[0].message.content
            } catch (e) {
                console.log(e);
                return null
            }
        } else {
            console.log('Request failed');
            console.log(response);
            return null
        }
    }

    #createBody(query) {
        return JSON.stringify({
            model: this.gptModel,
            messages: [
                {
                    role: 'user',
                    content: query
                }
            ]
        })
    }
}

export default GptApi;