import { useEffect, useState } from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { oneDark } from '@codemirror/theme-one-dark';
import { cpp } from '@codemirror/lang-cpp';
import { java } from '@codemirror/lang-java';
import { python } from '@codemirror/lang-python';
import { indentOnInput } from '@codemirror/language';
import { EditorState } from '@codemirror/state';


const CodeEditor = () => {
    const [code, setCode] = useState(`#include<bits/stdc++.h>

using namespace std;

int main()
{
  cout << "Hello world" << endl;
}

`);
    const [language, setLanguage] = useState("cpp");
    const [output, setOutput] = useState("");
    const [input, setInput] = useState("");
    const [socket, setSocket] = useState(null);

    useEffect(() => {
        const newSocket = new WebSocket("ws://localhost:8080/executeCpp");
        newSocket.onopen = () => {
            console.log("webSocket connection established succesfully...");
        }
        newSocket.onmessage = (event) => {
            console.log(event);
            setOutput((prev) => prev + event.data + "\n");
        }

        newSocket.onclose = () => {
            console.log("websocket connection closed...");
        }
        setSocket(newSocket);
    }, [])


    const getLanguage = (language) => {
        switch (language) {
            case "cpp": return cpp();
            case "java": return java();
            case "python": return python();
            default: return cpp();
        }
    }


    const sendInput = () => {
        console.log("input is sending to the server...");
        socket.send(input);
        console.log("input has been sent succesfully...");
        setInput("");
    }
    const handleRunCode = () => {
        // console.log("run code button clicked...");
        // console.log("code :", code);
        setOutput("");
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send("code:" + code);
        }
    }

    return (
        <div className="p-4">
            <div className="flex justify-around mb-4">
                <select
                    value={language}
                    onChange={(e) => setLanguage(e.target.value)}
                    className="border p-2 focus:outline-none"
                >
                    <option value="cpp">C++</option>
                    <option value="python">Python</option>
                    <option value="java">Java</option>
                </select>
                <button onClick={handleRunCode} className="bg-teal-400 rounded-md hover:bg-teal-500 text-white p-2">
                    Run
                </button>
            </div>
            <div className="flex sm:flex-row flex-col">
                <div className="flex-1 mr-4 w-full">
                    <CodeMirror
                        value={code}
                        theme={oneDark}
                        extensions={[
                            getLanguage(language),
                            indentOnInput()
                        ]}
                        onChange={(value) => setCode(value)}
                        height="60vh"
                        className='text-lg'
                    />
                </div>
                <div className="w-1/3 flex flex-col ">
                    <div className="mb-4 flex-1 ">
                        <h3 className="text-lg font-bold">Output</h3>
                        <textarea
                            readOnly
                            value={output}
                            className="sm:w-full w-[90vw] focus:outline-gray-300 h-40 border p-2"
                        />
                    </div>
                    <div className="flex-1">
                        <h3 className="text-lg font-bold">Input</h3>
                        <textarea
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            className="sm:w-full w-[90vw] h-20 border p-2 focus:outline-gray-300"
                        />
                        <button onClick={sendInput} className="bg-teal-600 text-white p-2 mt-2 sm:w-full w-[90vw] hover:bg-teal-700">
                            Send Input
                        </button>
                    </div>
                </div>
            </div>

        </div>
    );
};

export default CodeEditor;
