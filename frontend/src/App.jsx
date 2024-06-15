
import {BrowserRouter as Router,Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import Home from './pages/Home'
import About from './pages/About'
import Contact from './pages/Contact'

function App() {

  return (
    <Router>
      <div className='min-h-screen flex flex-col'>
        <Navbar/>
        <main className='flex flex-grow'>
          <Routes>
            <Route path='/' element={<Home/>}/>
            <Route path='/about' element={<About/>}/>
            <Route path='/contact' element={<Contact/>}/>
          </Routes>

        </main>
      </div>
    </Router>
  )
}

export default App
